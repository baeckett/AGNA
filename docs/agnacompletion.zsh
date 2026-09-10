#compdef agna
# agna CLI completion for zsh
# place this file on your fpath as _agna, e.g.:
#   mkdir -p ~/.zfunc && cp docs/agnacompletion.zsh ~/.zfunc/_agna
#   printf 'fpath=(~/.zfunc $fpath)\nautoload -Uz compinit && compinit\n' >> ~/.zshrc
#
# Completes commands, metric names, analyses, transform ops, layouts,
# formats, types, option values and file arguments.

_agna() {
  local -a commands analyses metric_names transform_ops layouts formats types
  commands=(
    'version:print the version'
    'info:network summary'
    'analyse:run analyses (--all, names, cliques:N, --out)'
    'convert:convert formats'
    'transform:apply an operation and save as a new network'
    'draw:render the network to PNG'
    'generate:generate a synthetic network'
    'matrix:print the adjacency matrix'
    'nodes:print the node table'
    'ego:extract a 1-hop ego network'
    'components:list connected components'
    'metrics:structured metrics (CSV/JSON)'
    'distance:shortest path between two nodes'
    'diff:structural comparison'
    '--help:this text'
    '--version:print the version'
  )
  analyses=(basic density cohesion nodal indegree outdegree emission reception determination status geodesics open-chain eccentricity diameter bavelas closeness fareness betweenness prestige cliques full)
  metric_names=(density diameter eccentricity closeness betweenness indegree outdegree total-degree emission reception status determination geodesics)
  transform_ops=(transpose symmetrize-max symmetrize-sum symmetrize-below normalize-binary normalize-threshold: add-scalar: multiply-scalar: square merge: delete-node: delete-nodes: isolate: merge-nodes: remove-outsiders)
  layouts=(circular random spring grid concentric)
  formats=(agn txt csv net graphml gml graphson xls)
  types=(random star circular)

  local -a opts
  case $words[2] in
    analyse)   opts=( $analyses '--all' '--out' ) ;;
    metrics)   opts=( $metric_names '--all' '--format' '--out' ) ;;
    transform) opts=( $transform_ops '--in-format' '--out-format' ) ;;
    draw)      opts=( '--layout' '--size' '--labels' '--out' ) ;;
    generate)  opts=( '--nodes' '--out' '--type' '--seed' '--degree' ) ;;
    convert)   opts=( '--in-format' '--out-format' ) ;;
    ego)       opts=( '--node' '--out' ) ;;
    distance)  opts=( '--from' '--to' ) ;;
    diff)      opts=( '--out' ) ;;
    *)         _describe 'command' commands; return ;;
  esac

  case $words[CURRENT-1] in
    --layout) compadd -- $layouts; return ;;
    --type)   compadd -- $types; return ;;
    --format) compadd -- csv tsv json; return ;;
    --in-format|--out-format) compadd -- $formats; return ;;
    --op)     compadd -- $transform_ops; return ;;
    --out|--node|--from|--to) _files; return ;;
    *)        compadd -- $opts; _files ;;
  esac
}

_agna "$@"